<#import "template.ftl" as layout />

<@layout.noauthentication>
    <section>
        <div class="container">
            <h2>Fridge #${fridge.id}: ${fridge.name}</h2>

            <#if message??>
                <p class="flash success">${message}</p>
            </#if>
            <#if error??>
                <p class="flash error">${error}</p>
            </#if>

            <div class="fridge-summary">
                <p><strong>ID:</strong> ${fridge.id}</p>
                <p><strong>Dimensions:</strong> ${fridge.width} × ${fridge.height} × ${fridge.depth}</p>
                <p><a href="/">Back to dashboard</a></p>
            </div>

            <section>
                <h3>Contents</h3>
                <#if contents?size == 0>
                    <p>No items placed in this fridge.</p>
                <#else>
                    <table>
                        <thead>
                            <tr><th>Item</th><th>Owner</th><th>Position</th><th>Action</th></tr>
                        </thead>
                        <tbody>
                            <#list contents as record>
                                <tr>
                                    <td>
                                        <#if itemMap[record.itemId?c]?has_content>
                                            #${record.itemId} - ${itemMap[record.itemId?c].name}
                                        <#else>
                                            Unknown item
                                        </#if>
                                    </td>
                                    <td>
                                        <#if itemMap[record.itemId?c]?has_content && ownerMap[itemMap[record.itemId?c].ownerId?c]?has_content>
                                            #${itemMap[record.itemId?c].ownerId} - ${ownerMap[itemMap[record.itemId?c].ownerId?c].name}
                                        <#else>
                                            Unknown owner
                                        </#if>
                                    </td>
                                    <td>${record.x}, ${record.y}, ${record.z}</td>
                                    <td>
                                        <form method="post" action="/fridge-records/${record.id}/delete" class="inline-form">
                                            <button type="submit">Remove</button>
                                        </form>
                                    </td>
                                </tr>
                            </#list>
                        </tbody>
                    </table>
                </#if>
            </section>

            <section>
                <h3>Place item</h3>
                <form method="post" action="/fridges/${fridge.id}/place-item" class="form-panel">
                    <label>Item
                        <select name="itemId" required>
                            <option value="">Select item</option>
                            <#list items as item>
                                <option value="${item.id}">
                                    #${item.id} - ${item.name}
                                    <#if ownerMap[item.ownerId?c]?has_content>
                                        (owner #${item.ownerId} - ${ownerMap[item.ownerId?c].name})
                                    <#else>
                                        (owner #${item.ownerId})
                                    </#if>
                                </option>
                            </#list>
                        </select>
                    </label>
                    <label>X <input type="number" name="x" min="0" max="${fridge.width - 1}" required></label>
                    <label>Y <input type="number" name="y" min="0" max="${fridge.height - 1}" required></label>
                    <label>Z <input type="number" name="z" min="0" max="${fridge.depth - 1}" required></label>
                    <button type="submit">Place item</button>
                </form>
            </section>
        </div>
    </section>
</@layout.noauthentication>
